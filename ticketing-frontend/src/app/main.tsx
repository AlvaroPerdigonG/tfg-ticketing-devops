import React from "react";
import ReactDOM from "react-dom/client";
import { ConfigProvider } from "antd";
import esES from "../vendor/antd/locale/es_ES";
import "../vendor/antd/reset.css";
import { App } from "./App";
import "./index.css";
import { AuthProvider } from "./providers/AuthProvider";

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={esES}
      theme={{
        token: {
          colorPrimary: "#0c9136",
          colorInfo: "#0c9136",
        },
      }}
    >
      <AuthProvider>
        <App />
      </AuthProvider>
    </ConfigProvider>
  </React.StrictMode>,
);
