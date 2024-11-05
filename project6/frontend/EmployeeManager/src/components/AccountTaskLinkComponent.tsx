import React from "react";
import Task from "../types/Task";
import AccountTaskLink from "../types/AccountTaskLink";
import { Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

function AccountTaskLinkComponent(props: AccountTaskLink) {
  const navigate = useNavigate();

  return (
    <div className="container">
      <div className="row mx-1 my-2 justify-content-center">
        <div className="col-6">
          <p>title: {props.taskTitle}</p>
          <Button
            variant="outline-secondary"
            onClick={() =>
              navigate("/task", { state: { taskUuid: props.taskUuid } })
            }
          >
            open
          </Button>
        </div>
      </div>
    </div>
  );
}

export default AccountTaskLinkComponent;
