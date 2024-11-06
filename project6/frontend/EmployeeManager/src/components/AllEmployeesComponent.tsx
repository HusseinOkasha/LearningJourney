import { useEffect, useState } from "react";
import { Account } from "../types/Account";
import axios, { AxiosResponse } from "axios";
import AccountCardComponent from "./AccountCardComponent";

function AllEmployeesComponent() {
  const [admins, setAdmins] = useState<Account[]>([]);
  const [feedback, setFeedback] = useState({});

  useEffect(() => {
    const fetchData = async () => await fetchAllAdmins();
    fetchData();
  }, []);

  // method encapuslates fetching all admins.
  const fetchAllAdmins = async () => {
    // get the access token from the local storage.
    const accessToken: string = localStorage.getItem("accessToken");

    // construct the url.
    const url: string = "http://localhost:8080/api/admin/employees/all";
    try {
      // send the request.
      const response: AxiosResponse = await axios.get(url, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      setAdmins(response.data);
    } catch (error) {
      if (error.status == 401) {
        setFeedback({
          error:
            "Your are unauthorized to access this page, please login with admin account to access it.",
        });
      } else {
        setFeedback({ error: error.message });
      }

      console.log(error);
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

export default AllEmployeesComponent;
