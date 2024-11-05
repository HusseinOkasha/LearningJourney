import axios, { AxiosResponse } from "axios";
import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import Task from "../types/Task";
import TaskComponent from "./AccountTaskLinkComponent";
import AccountTaskLink from "../types/AccountTaskLink";
import AccountTaskLinkComponent from "./AccountTaskLinkComponent";

function AccountTasks() {
  // extract the account uuid.
  const location = useLocation();
  const accountUuid: string = location.state?.accountUuid;

  // states
  const [accountTasks, setAccountTasks] = useState<AccountTaskLink[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      await fetchAccountTasks();
    };
    fetchData();
  }, []);

  const fetchAccountTasks = async () => {
    // construct the url.
    const url = `http://localhost:8080/api/account/${accountUuid}/tasks`;

    // get the access token from local storage
    const accessToken = localStorage.getItem("accessToken");

    // send the request.
    const response: AxiosResponse = await axios.get(url, {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    console.log(response);
    // update the state
    setAccountTasks(response.data);

    console.log(accountTasks);
  };

  return (
    <div>
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

export default AccountTasks;
