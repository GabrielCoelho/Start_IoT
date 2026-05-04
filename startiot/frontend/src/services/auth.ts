export const validateLogin= (email: string, password: string, role: string) => {
  const MOCK_USER = "operador@fatec.sp.gov.br";
  const MOCK_PASS = "123456";

  if (email === MOCK_USER && password === MOCK_PASS) {
    return {
      success: true,
      user: { name: "Operador FATEC", role: role }
    };
  }

  return { success: false, message: "E-mail ou senha incorretos!" };
};