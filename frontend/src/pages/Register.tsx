import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { register } from "@/services/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useToast } from "@/components/ui/use-toast";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [lastname, setLastname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { toast } = useToast();
  const navigate = useNavigate();

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      await register({ name, lastname, email, password });
      toast({ title: "Registro realizado" });
      navigate("/login");
    } catch (err: any) {
      toast({ title: "Falha no registro", description: err?.message ?? "Erro", variant: "destructive" });
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <form onSubmit={onSubmit} className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-semibold">Criar conta</h1>
        <div className="space-y-2">
          <label className="text-sm">Nome</label>
          <Input value={name} onChange={e => setName(e.target.value)} />
        </div>
        <div className="space-y-2">
          <label className="text-sm">Sobrenome</label>
          <Input value={lastname} onChange={e => setLastname(e.target.value)} />
        </div>
        <div className="space-y-2">
          <label className="text-sm">Email</label>
          <Input type="email" value={email} onChange={e => setEmail(e.target.value)} />
        </div>
        <div className="space-y-2">
          <label className="text-sm">Senha</label>
          <Input type="password" value={password} onChange={e => setPassword(e.target.value)} />
        </div>
        <Button type="submit" className="w-full">Registrar</Button>
        <p className="text-sm text-muted-foreground">
          Já tem conta? <Link to="/login" className="underline">Entrar</Link>
        </p>
      </form>
    </div>
  );
}
