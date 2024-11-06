import { useNavigate } from "react-router-dom";
import { Account } from "../types/Account";

import { Button } from "react-bootstrap";

function AccountCardComponent(props: Account) {
  const navigate = useNavigate();
  return (
    <div className="container">
      <div className="row mx-1 my-2 justify-content-center">
        <div className="col-6">
          <p>Email: {props.email}</p>
          <p>Name: {props.name}</p>
          <p>role: {props.role}</p>
          <p>
            <Button
              variant="outline-secondary"
              onClick={() =>
                navigate("/account/tasks", {
                  state: { accountUuid: props.accountUuid },
                })
              }
            >
              view tasks
            </Button>
          </p>
        </div>
      </div>
    </div>
  );
}

export default AccountCardComponent;
