import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App.tsx";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap/dist/js/bootstrap.bundle.min";
import LoginComponent from "./components/LoginComponent.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <LoginComponent />
  </StrictMode>
);
