import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import { message } from "antd";
import type { User } from "../types";
import { login as loginApi, register as registerApi, getMe, logout as logoutApi } from "../api";

interface AuthState {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (phone: string, password: string) => Promise<void>;
  register: (phone: string, password: string, nickname: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  /** 初始化时尝试恢复会话 */
  useEffect(() => {
    getMe()
      .then((res) => {
        if (res.code === 200 && res.data) {
          setUser(res.data);
        }
      })
      .catch(() => {
        // 未登录或 session 过期，忽略
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (phone: string, password: string) => {
    const res = await loginApi({ phone, password });
    if (res.code !== 200) {
      throw new Error(res.message || "登录失败");
    }
    setUser(res.data);
    message.success("登录成功");
  }, []);

  const register = useCallback(async (phone: string, password: string, nickname: string) => {
    const res = await registerApi({ phone, password, nickname });
    if (res.code !== 200) {
      throw new Error(res.message || "注册失败");
    }
    setUser(res.data);
    message.success("注册成功");
  }, []);

  const logout = useCallback(async () => {
    await logoutApi();
    setUser(null);
    message.success("已退出登录");
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated: user !== null,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

/** Hook: 获取认证状态 */
export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth 必须在 AuthProvider 内部使用");
  }
  return ctx;
}
