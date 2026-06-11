import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Logo } from "@/components/Logo";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { ArrowLeft, ShieldCheck, User } from "lucide-react";
import { getStoredAuth } from "@/services/authStorage";

const Profile = () => {
  const navigate = useNavigate();
  const auth = getStoredAuth();
  const rawRol = auth?.role?.toString().toUpperCase() || "GENERADOR";
  const userRole = rawRol === "ADMIN" ? "admin" : (rawRol === "RECOLECTOR" ? "recolector" : "empresa");

  const [user, setUser] = useState({
    name: auth?.companyName || "Usuario",
    sub: auth?.email || "No autenticado",
  });

  useEffect(() => {
    if (auth) {
      setUser({
        name: auth.companyName,
        sub: auth.email,
      });
    }
  }, []);

  return (
    <div className="min-h-screen bg-muted/40">
      <header className="bg-card border-b border-border sticky top-0 z-30">
        <div className="container py-4 flex items-center justify-between">
          <Logo />
          <Button variant="ghost" onClick={() => navigate(-1)} size="sm"><ArrowLeft className="h-4 w-4 mr-1.5"/> Volver</Button>
        </div>
      </header>

      <div className="container py-8 max-w-5xl">
        <div className="mb-6">
          <Badge className="bg-secondary text-secondary-foreground mb-2">Mi cuenta</Badge>
          <h1 className="font-display text-3xl font-bold">Perfil y configuración</h1>
          <p className="text-sm text-muted-foreground">Consulta los datos de tu cuenta institucional autorizada. Los cambios están diferidos para esta versión.</p>
        </div>

        <Card className="p-6">
          {userRole === "empresa" && (
            <>
              <div className="flex items-center justify-between mb-5">
                <div>
                  <h3 className="font-display font-bold text-lg">Datos de la empresa</h3>
                  <p className="text-xs text-muted-foreground">Información de cuenta registrada</p>
                </div>
                <Badge className="bg-success text-success-foreground"><ShieldCheck className="h-3 w-3 mr-1"/> Activa</Badge>
              </div>
              <div className="grid sm:grid-cols-2 gap-4">
                <Field label="Razón social" value={user.name} disabled/>
                <Field label="RUC" value="—" disabled/>
                <Field label="Nombre comercial" value={user.name} disabled/>
                <Field label="Rubro" value="Restaurantes, bares y cantinas" disabled/>
                <Field label="email registrado" value={user.sub} disabled/>
                <Field label="Estado" value="AUTORIZADO" disabled/>
              </div>
            </>
          )}
          {userRole === "recolector" && (
            <>
              <div className="flex items-center justify-between mb-5">
                <div>
                  <h3 className="font-display font-bold text-lg">Datos de empresa recolectora</h3>
                  <p className="text-xs text-muted-foreground">Cobertura operativa y contacto</p>
                </div>
                <Badge className="bg-success text-success-foreground">Autorizado</Badge>
              </div>
              <div className="grid sm:grid-cols-2 gap-4">
                <Field label="Razón social" value={user.name} disabled/>
                <Field label="Responsable" value="Pedro Catacora Mamani" disabled/>
                <Field label="email operativo" value={user.sub} disabled/>
                <Field label="Estado" value="ACTIVO" disabled/>
              </div>
            </>
          )}
          {userRole === "admin" && (
            <>
              <div className="flex items-center justify-between mb-5">
                <div>
                  <h3 className="font-display font-bold text-lg">Datos del administrador</h3>
                  <p className="text-xs text-muted-foreground">Acceso por email institucional</p>
                </div>
                <Badge variant="outline" className="border-info/40 text-info">Acceso institucional</Badge>
              </div>
              <div className="grid sm:grid-cols-2 gap-4">
                <Field label="Nombre completo" value={user.name} disabled/>
                <Field label="email institucional" value={user.sub} disabled/>
                <Field label="Área" value="Gestión de residuos sólidos" disabled/>
              </div>
            </>
          )}
          <div className="mt-6 flex justify-end">
            <Button className="bg-gradient-eco" disabled title="Edición de perfil diferida para una fase posterior">Guardar cambios</Button>
          </div>
        </Card>
      </div>
    </div>
  );
};

interface FieldProps {
  label: string;
  value: string;
  type?: string;
  disabled?: boolean;
  placeholder?: string;
  className?: string;
}

const Field = ({ label, value, type = "text", disabled, placeholder, className = "" }: FieldProps) => (
  <div className={`space-y-1.5 ${className}`}>
    <Label className="text-xs">{label}</Label>
    <Input type={type} defaultValue={value} disabled={disabled} placeholder={placeholder} className={`h-11 ${disabled ? "bg-muted/50" : ""}`}/>
  </div>
);

export default Profile;
