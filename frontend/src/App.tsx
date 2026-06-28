import { useState } from 'react';
import LoginPage from './pages/LoginPage';
import StuffedAnimalPage from './pages/StuffedAnimalPage';

function App() {
  // トークンがあればログイン済みとみなす
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('accessToken'));

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
  };

  if (!isLoggedIn) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  return <StuffedAnimalPage />;
}

export default App;