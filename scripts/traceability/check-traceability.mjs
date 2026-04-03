#!/usr/bin/env node

import { promises as fs } from 'node:fs';
import path from 'node:path';
import process from 'node:process';

const ROOT_DIR = process.cwd();
const FEATURE_DIR = path.join(ROOT_DIR, 'docs', 'features');
const OUTPUT_DIR = path.join(ROOT_DIR, 'artifacts', 'traceability');
const OUTPUT_JSON = path.join(OUTPUT_DIR, 'traceability-report.json');
const OUTPUT_MD = path.join(OUTPUT_DIR, 'traceability-report.md');

const SEARCH_TARGETS = {
  backend: {
    label: 'Backend tests',
    baseDir: path.join(ROOT_DIR, 'ticketing-backend', 'src', 'test'),
    include: (filePath) => /\.(java|kt|groovy)$/i.test(filePath),
  },
  frontendUi: {
    label: 'Frontend unit/UI tests',
    baseDir: path.join(ROOT_DIR, 'ticketing-frontend', 'src'),
    include: (filePath) => /\.(test|spec)\.(t|j)sx?$/i.test(filePath),
  },
  e2e: {
    label: 'Frontend E2E',
    baseDir: path.join(ROOT_DIR, 'ticketing-frontend', 'e2e'),
    include: (filePath) => /\.spec\.(t|j)sx?$/i.test(filePath),
  },
};

const SCENARIO_ID_REGEX = /\b[A-Z]+(?:-[A-Z]+)*-\d{2}\b/g;

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

async function safeReadDir(dir) {
  try {
    return await fs.readdir(dir, { withFileTypes: true });
  } catch {
    return [];
  }
}

async function walkFiles(baseDir, includeFn) {
  const collected = [];

  async function walk(currentDir) {
    const entries = await safeReadDir(currentDir);

    for (const entry of entries) {
      const resolved = path.join(currentDir, entry.name);

      if (entry.isDirectory()) {
        await walk(resolved);
        continue;
      }

      if (!entry.isFile()) {
        continue;
      }

      if (includeFn(resolved)) {
        collected.push(resolved);
      }
    }
  }

  await walk(baseDir);
  return collected;
}

async function loadFeatureScenarios() {
  const featureFiles = (await safeReadDir(FEATURE_DIR))
    .filter((entry) => entry.isFile() && entry.name.endsWith('.feature'))
    .map((entry) => path.join(FEATURE_DIR, entry.name));

  const scenarios = new Map();

  for (const featureFile of featureFiles) {
    const content = await fs.readFile(featureFile, 'utf-8');
    const matches = content.match(SCENARIO_ID_REGEX) ?? [];

    for (const id of matches) {
      if (!scenarios.has(id)) {
        scenarios.set(id, {
          id,
          featureExists: true,
          featureFile: path.relative(ROOT_DIR, featureFile),
        });
      }
    }
  }

  return {
    featureFilesCount: featureFiles.length,
    scenarios: [...scenarios.values()].sort((a, b) => a.id.localeCompare(b.id)),
  };
}

async function findReferencesById(ids, target) {
  const files = await walkFiles(target.baseDir, target.include);
  const occurrences = new Map(ids.map((id) => [id, []]));

  for (const filePath of files) {
    const text = await fs.readFile(filePath, 'utf-8').catch(() => '');
    if (!text) continue;

    for (const id of ids) {
      const pattern = new RegExp(`\\b${escapeRegExp(id)}\\b`);
      if (pattern.test(text)) {
        occurrences.get(id).push(path.relative(ROOT_DIR, filePath));
      }
    }
  }

  return {
    filesScanned: files.length,
    occurrences,
  };
}

function resolveStatus(layers) {
  const matchedLayers = [layers.backend, layers.frontendUi, layers.e2e].filter(Boolean).length;

  if (matchedLayers === 0) return 'NOT_REFERENCED';
  if (matchedLayers === 3) return 'MULTI_LAYER';
  return 'PARTIAL';
}

function buildMarkdown(report) {
  const lines = [];
  lines.push('# Traceability report');
  lines.push('');
  lines.push(`Generated at: ${report.timestamp}`);
  lines.push('');
  lines.push('| Scenario ID | Feature | Backend | Frontend UI | E2E | Status |');
  lines.push('| --- | --- | --- | --- | --- | --- |');

  for (const scenario of report.scenarios) {
    lines.push(
      `| ${scenario.id} | ${scenario.featureExists ? '✅' : '❌'} | ${scenario.backend ? '✅' : '❌'} | ${scenario.frontendUi ? '✅' : '❌'} | ${scenario.e2e ? '✅' : '❌'} | ${scenario.status} |`,
    );
  }

  lines.push('');
  lines.push('## Summary');
  lines.push(`- Total scenarios: **${report.counters.totalScenarios}**`);
  lines.push(`- NOT_REFERENCED: **${report.counters.NOT_REFERENCED}**`);
  lines.push(`- PARTIAL: **${report.counters.PARTIAL}**`);
  lines.push(`- MULTI_LAYER: **${report.counters.MULTI_LAYER}**`);

  return `${lines.join('\n')}\n`;
}

async function main() {
  const timestamp = new Date().toISOString();
  const { featureFilesCount, scenarios } = await loadFeatureScenarios();
  const scenarioIds = scenarios.map((scenario) => scenario.id);

  const backendResult = await findReferencesById(scenarioIds, SEARCH_TARGETS.backend);
  const frontendUiResult = await findReferencesById(scenarioIds, SEARCH_TARGETS.frontendUi);
  const e2eResult = await findReferencesById(scenarioIds, SEARCH_TARGETS.e2e);

  const enriched = scenarios.map((scenario) => {
    const backendMatches = backendResult.occurrences.get(scenario.id) ?? [];
    const frontendUiMatches = frontendUiResult.occurrences.get(scenario.id) ?? [];
    const e2eMatches = e2eResult.occurrences.get(scenario.id) ?? [];

    const backend = backendMatches.length > 0;
    const frontendUi = frontendUiMatches.length > 0;
    const e2e = e2eMatches.length > 0;
    const status = resolveStatus({ backend, frontendUi, e2e });

    return {
      ...scenario,
      backend,
      frontendUi,
      e2e,
      status,
      references: {
        backend: backendMatches,
        frontendUi: frontendUiMatches,
        e2e: e2eMatches,
      },
    };
  });

  const counters = {
    totalScenarios: enriched.length,
    NOT_REFERENCED: enriched.filter((entry) => entry.status === 'NOT_REFERENCED').length,
    PARTIAL: enriched.filter((entry) => entry.status === 'PARTIAL').length,
    MULTI_LAYER: enriched.filter((entry) => entry.status === 'MULTI_LAYER').length,
  };

  const report = {
    timestamp,
    metadata: {
      featureDirectory: path.relative(ROOT_DIR, FEATURE_DIR),
      featureFilesCount,
      scanned: {
        backendFiles: backendResult.filesScanned,
        frontendUiFiles: frontendUiResult.filesScanned,
        e2eFiles: e2eResult.filesScanned,
      },
      mode: {
        strict: false,
        note: 'Scaffold mode: does not fail process on partial coverage.',
      },
    },
    counters,
    scenarios: enriched,
  };

  await fs.mkdir(OUTPUT_DIR, { recursive: true });
  await fs.writeFile(OUTPUT_JSON, `${JSON.stringify(report, null, 2)}\n`, 'utf-8');
  await fs.writeFile(OUTPUT_MD, buildMarkdown(report), 'utf-8');

  console.log('Traceability scaffold report generated.');
  console.log(`- Scenarios: ${report.counters.totalScenarios}`);
  console.log(`- NOT_REFERENCED: ${report.counters.NOT_REFERENCED}`);
  console.log(`- PARTIAL: ${report.counters.PARTIAL}`);
  console.log(`- MULTI_LAYER: ${report.counters.MULTI_LAYER}`);
  console.log(`- JSON: ${path.relative(ROOT_DIR, OUTPUT_JSON)}`);
  console.log(`- Markdown: ${path.relative(ROOT_DIR, OUTPUT_MD)}`);
}

main().catch((error) => {
  console.error('Unexpected error while generating traceability report (non-blocking scaffold):');
  console.error(error);
  process.exitCode = 0;
});
