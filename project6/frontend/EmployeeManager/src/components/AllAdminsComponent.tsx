import axios, { AxiosResponse } from "axios";
import { useEffect, useState } from "react";
import { Account } from "../types/Account";
import AccountCardComponent from "./AccountCardComponent";

function AllAdminsComponent() {
  const [admins, setAdmins] = useState<Account[]>([]);

  useEffect(() => {
    const fetchData = async () => await fetchAllAdmins();
    fetchData();
  }, []);

  const fetchAllAdmins = async () => {
    // get the access token from the local storage.
    const accessToken: string = localStorage.getItem("accessToken");

    // construct the url.
    const url: string = "http://localhost:8080/api/admin/all";

    // send the request.
    const response: AxiosResponse = await axios.get(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
    setAdmins(response.data);
  };

  return (
    <div className="container">
      {admins.map((admin) => (
        <AccountCardComponent
          name={admin.name}
          email={admin.email}
          accountUuid={admin.accountUuid}
          role={admin.role}
          key={admin.accountUuid}
        />
      ))}
    </div>
  );
}

export default AllAdminsComponent;
