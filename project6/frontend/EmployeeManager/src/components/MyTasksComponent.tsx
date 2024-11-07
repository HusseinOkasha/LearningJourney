import axios, { AxiosResponse } from "axios";
import { useEffect, useState } from "react";
import AccountTaskLinkComponent from "./AccountTaskLinkComponent";
import AccountTaskLink from "../types/AccountTaskLink";

function MyTasksComponent() {
  // states
  const [accountTasks, setAccountTasks] = useState<AccountTaskLink[]>([]);
  const [feedback, setFeedback] = useState({});

  useEffect(() => {
    const fetchData = async () => {
      await fetchMyTasks();
    };
    fetchData();
  }, []);

  const fetchMyTasks = async () => {
    // construct the url.
    const url = `http://localhost:8080/api/account/tasks`;

    // get the access token from local storage
    const accessToken = localStorage.getItem("accessToken");

    try {
      // send the request.
      const response: AxiosResponse = await axios.get(url, {
        headers: { Authorization: `Bearer ${accessToken}` },
      });
      console.log(response);
      // update the state
      setAccountTasks(response.data);

      console.log(accountTasks);
    } catch (error) {
      if (error.status == 401) {
        setFeedback({
          error:
            "Your are unauthorized to access this page, please login with admin account to access it.",
        });
      } else {
        setFeedback({ error: error });
      }
    }
  };

  return (
    <div className="container">
      <div className="row justify-content-center my-2">
        <div className="col-6">
          {feedback.error && (
            <div className="alert alert-danger">{feedback.error}</div>
          )}
        </div>
      </div>
      {accountTasks.map((accountTask) => (
        <AccountTaskLinkComponent
          taskTitle={accountTask.taskTitle}
          taskUuid={accountTask.taskUuid}
          accountName={accountTask.accountName}
          accountUuid={accountTask.accountUuid}
        />
      ))}
    </div>
  );
}

export default MyTasksComponent;
