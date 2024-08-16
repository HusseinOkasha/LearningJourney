import { BrowserRouter, Route, Routes } from "react-router-dom";
import CreateUserProfileComponent from "./components/CreateUserProfileComponent";
import DropZoneComponent from "./components/DropZoneComponent";
import UserProfile from "./components/UserProfile";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/">
          <Route index element={<CreateUserProfileComponent />} />
          <Route path="/profile/image/upload" element={<DropZoneComponent />} />
          <Route path="/profile" element={<UserProfile />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
