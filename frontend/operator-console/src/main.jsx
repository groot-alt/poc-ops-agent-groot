import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./app/App.jsx";
import { AppProviders } from "./app/providers.jsx";
import "./styles/global.css";

const root = document.getElementById("root");

if (!root) {
  throw new Error("Operator console root element is missing");
}

createRoot(root).render(
  <StrictMode>
    <AppProviders>
      <App />
    </AppProviders>
  </StrictMode>,
);
