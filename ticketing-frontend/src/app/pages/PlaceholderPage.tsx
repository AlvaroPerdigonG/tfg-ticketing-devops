import { Card, Typography } from "antd";

type PlaceholderPageProps = {
  title: string;
};

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <Card>
      <Typography.Title level={3} style={{ marginTop: 0 }}>
        {title}
      </Typography.Title>
      <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
        Página placeholder para el MVP.
      </Typography.Paragraph>
    </Card>
  );
}
