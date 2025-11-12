import { useState } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { login } from "@/services/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useToast } from "@/components/ui/use-toast";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { toast } = useToast();
  const navigate = useNavigate();
  const location = useLocation() as any;
  const from = location.state?.from?.pathname || "/";

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      await login({ email, password });
      toast({ title: "Login realizado" });
      navigate(from, { replace: true });
    } catch (err: any) {
      toast({ title: "Falha no login", description: err?.message ?? "Erro", variant: "destructive" });
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <form onSubmit={onSubmit} className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-semibold">Entrar</h1>
        <div className="space-y-2">
          <label className="text-sm">E-mail</label>
          <Input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="seu@email.com" />
        </div>
        <div className="space-y-2">
          <label className="text-sm">Senha</label>
          <Input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" />
        </div>
        <Button type="submit" className="w-full">Entrar</Button>
        <p className="text-sm text-muted-foreground">
          Não tem conta? <Link to="/register" className="underline">Registre-se</Link>
        </p>
      </form>
    </div>
  );
}