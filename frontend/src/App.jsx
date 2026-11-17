import { Route, Routes } from "react-router-dom";
import { ErrorBoundary } from "react-error-boundary";
import AppNavbar from "./AppNavbar";
import Home from "./home";
import PrivateRoute from "./privateRoute";
import Register from "./auth/register";
import Login from "./auth/login";
import Logout from "./auth/logout";
import PlanList from "./public/plan";
import useAuth from "./hooks/useAuth";
import UserListAdmin from "./admin/users/UserListAdmin";
import UserEditAdmin from "./admin/users/UserEditAdmin";
import UserCreateAdmin from "./admin/users/UserCreateAdmin";
import UserDetailAdmin from "./admin/users/UserDetailAdmin";
import SwaggerDocs from "./public/swagger";
import CreateGame from "./game/CreateGame";
import JoinGame from "./game/JoinGame";
import SelectCharacter from "./game/SelectCharacter";
import Lobby from "./game/Lobby";
import GamePlay from "./game/GamePlay";
import Board from "./board/Board";
import ActiveGamesAdmin from "./admin/games/ActiveGamesAdmin";
import GameHistory from "./game/GameHistory";
import Profile from "./profile/Profile";
import PlayerDirectory from "./community/PlayerDirectory";
import UserHub from "./hub/UserHub";
import SocialHub from "./hub/SocialHub";

function ErrorFallback({ error, resetErrorBoundary }) {
  return (
    <div role="alert">
      <p>Something went wrong:</p>
      <pre>{error.message}</pre>
      <button onClick={resetErrorBoundary}>Try again</button>
    </div>
  )
}

function App() {
  const { jwt, roles } = useAuth();

  let adminRoutes = <></>;
  let playerRoutes = <></>;
  let userRoutes = <></>;
  let publicRoutes = <></>;

  roles.forEach((role) => {
    if (role === "ADMIN") {
      adminRoutes = (
        <>
          <Route path="/users" exact={true} element={<PrivateRoute><UserListAdmin /></PrivateRoute>} />
          <Route path="/users/new" exact={true} element={<PrivateRoute><UserCreateAdmin /></PrivateRoute>} />
          <Route path="/users/:id/edit" exact={true} element={<PrivateRoute><UserEditAdmin /></PrivateRoute>} />
          <Route path="/users/:id" exact={true} element={<PrivateRoute><UserDetailAdmin /></PrivateRoute>} />
        </>)
    }
    if (role === "PLAYER") {
      playerRoutes = (
        <>

        </>)
    }
  })
  if (!jwt) {
    publicRoutes = (
      <>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
      </>
    )
  } else {
    userRoutes = (
      <>
        <Route path="/games/active" element={<PrivateRoute><ActiveGamesAdmin /></PrivateRoute>} />
        <Route path="/games/history" element={<PrivateRoute><GameHistory /></PrivateRoute>} />
        <Route path="/community" element={<PrivateRoute><PlayerDirectory /></PrivateRoute>} />
        <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
        <Route path="/logout" element={<Logout />} />
        <Route path="/login" element={<Login />} />
      </>
    )
  }

  return (
    <div>
      <ErrorBoundary FallbackComponent={ErrorFallback} >
        <AppNavbar />
        <Routes>
          <Route path="/" exact={true} element={<Home />} />
          <Route path="/create-game" element={<CreateGame />} />
          <Route path="/join-game" element={<JoinGame />} />
          <Route path="/select-character/:gameCode" element={<SelectCharacter />} />
          <Route path="/game/lobby/:gameCode" element={<Lobby />} />
          <Route path="/game/play/:gameCode" element={<GamePlay />} />
          <Route path="/board" element={<Board />} />
          <Route path="/plans" element={<PlanList />} />
          <Route path="/docs" element={<SwaggerDocs />} />
          <Route path="/user-hub" element={<PrivateRoute><UserHub /></PrivateRoute>} />
          <Route path="/social-hub" element={<PrivateRoute><SocialHub /></PrivateRoute>} />
          {publicRoutes}
          {userRoutes}
          {adminRoutes}
          {playerRoutes}
        </Routes>
      </ErrorBoundary>
    </div>
  );
}

export default App;
