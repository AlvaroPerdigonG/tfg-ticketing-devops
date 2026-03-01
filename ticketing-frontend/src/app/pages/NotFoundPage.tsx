import { Button, Result } from "antd";
import { useNavigate } from "react-router-dom";

export function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <Result
      status="404"
      title="404"
      subTitle="La página que buscas no existe."
      extra={<Button onClick={() => navigate("/dashboard")}>Ir al dashboard</Button>}
    />
  );
}
