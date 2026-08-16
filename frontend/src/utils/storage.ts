const TOKEN_KEY = 'short-link-token'
const USERNAME_KEY = 'short-link-username'

export const storage = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clearToken: () => localStorage.removeItem(TOKEN_KEY),
  getUsername: () => localStorage.getItem(USERNAME_KEY) ?? '',
  setUsername: (name: string) => localStorage.setItem(USERNAME_KEY, name),
  clearUsername: () => localStorage.removeItem(USERNAME_KEY)
}
