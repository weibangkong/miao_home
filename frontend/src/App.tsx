import { Routes, Route, useLocation } from "react-router-dom";
import CatList from "./pages/CatList";
import CatDetail from "./pages/CatDetail";
import CatForm from "./pages/CatForm";
import AdopterList from "./pages/AdopterList";
import NotificationList from "./pages/NotificationList";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";
import Register from "./pages/Register";
import RequireAuth from "./components/RequireAuth";
import GuestOnly from "./components/GuestOnly";
import { AppSidebar, AppHeader } from "./components/layout";
import { useAuth } from "./contexts/AuthContext";

export default function App() {
  const { isAuthenticated, loading: authLoading } = useAuth();
  const location = useLocation();

  const isAuthPage = location.pathname === "/login" || location.pathname === "/register";
  const showChrome = isAuthenticated && !isAuthPage && !authLoading;

  return (
    <div style={{ minHeight: "100vh", background: "#F5F5F5" }}>
      {showChrome && <AppSidebar />}

      <div className={showChrome ? "app-main" : ""}>
        {showChrome && <AppHeader />}

        <div className="app-content">
          <Routes>
            <Route path="/" element={<RequireAuth><Dashboard /></RequireAuth>} />
            <Route path="/cats" element={<RequireAuth><CatList /></RequireAuth>} />
            <Route path="/cats/new" element={<RequireAuth><CatForm /></RequireAuth>} />
            <Route path="/cats/:id" element={<RequireAuth><CatDetail /></RequireAuth>} />
            <Route path="/cats/:id/edit" element={<RequireAuth><CatForm /></RequireAuth>} />
            <Route path="/adopters" element={<RequireAuth><AdopterList /></RequireAuth>} />
            <Route path="/notifications" element={<RequireAuth><NotificationList /></RequireAuth>} />
            <Route path="/login" element={<GuestOnly><Login /></GuestOnly>} />
            <Route path="/register" element={<GuestOnly><Register /></GuestOnly>} />
          </Routes>
        </div>
      </div>
    </div>
  );
}
