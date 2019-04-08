package com.stars.multiserver.fight.data;

/**
 * Created by zhouxiaogang on 2016/12/13.
 */
public class Vector3 {
    public double x;
    public double y;
    public double z;

    public Vector3(){}
    public Vector3(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(String posStr){
        if(posStr != null){
            String[] valArr = posStr.split("\\+");
            if(valArr.length == 3){
                this.x = Double.parseDouble(valArr[0]);
                this.y = Double.parseDouble(valArr[1]);
                this.z = Double.parseDouble(valArr[2]);
            }
        }
    }

    public Vector3 clone(){
        return new Vector3(this.x, this.y, this.z);
    }

    public double magnitude(){
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vector3 max(Vector3 lhs, Vector3 rhs){
        return new Vector3(Math.max(lhs.x, rhs.x), Math.max(lhs.y, rhs.y), Math.max(lhs.z, rhs.z));
    }

    public Vector3 min(Vector3 lhs, Vector3 rhs){
        return new Vector3(Math.min(lhs.x, rhs.x), Math.min(lhs.y, rhs.y), Math.min(lhs.z, rhs.z));
    }

    public Vector3 setNormalize(){
        double num = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        if (num > 1e-5) {
            this.x = this.x / num;
            this.y = this.y / num;
            this.z = this.z / num;
        }
        else{
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
        return this;
    }

    public double sqrMagnitude(){
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double sqrMagnitudeWithoutY(){
        return this.x * this.x + this.z * this.z;
    }

    public Vector3 clampMagnitude(double maxLength){
        if (this.sqrMagnitude() > (maxLength * maxLength)){
            this.setNormalize();
            this.mul(maxLength);
        }
        return this;
    }

    public Vector3 mul(double q){
        this.x = this.x * q;
        this.y = this.y * q;
        this.z = this.z * q;
        return this;
    }

    public Vector3 div(double d){
        this.x = this.x / d;
        this.y = this.y / d;
        this.z = this.z / d;
        return this;
    }

    public Vector3 add(Vector3 vb){
        this.x = this.x + vb.x;
        this.y = this.y + vb.y;
        this.z = this.z + vb.z;
        return this;
    }

    public Vector3 sub(Vector3 vb){
        this.x = this.x - vb.x;
        this.y = this.y - vb.y;
        this.z = this.z - vb.z;
        return this;
    }

    public Vector3 mulQuat(double rx, double ry, double rz, double rw){
        double num 	= rx * 2;
        double num2 = ry * 2;
        double num3 = rz * 2;
        double num4 = rx * num;
        double num5 = ry * num2;
        double num6 = rz * num3;
        double num7 = rx * num2;
        double num8 = rx * num3;
        double num9 = ry * num3;
        double num10 = rw * num;
        double num11 = rw * num2;
        double num12 = rw * num3;
        this.x = (((1 - (num5 + num6)) * this.x) + ((num7 - num12) * this.y)) + ((num8 + num11) * this.z);
        this.y = (((num7 + num12) * this.x) + ((1 - (num4 + num6)) * this.y)) + ((num9 - num10) * this.z);
        this.z = (((num8 - num11) * this.x) + ((num9 + num10) * this.y)) + ((1 - (num4 + num5)) * this.z);
        return this;
    }

    public double angleAroundAxis(Vector3 from, Vector3 to, Vector3 axis){
        from = Vector3.sub(from, Vector3.project(from, axis));
        to = Vector3.sub(to, Vector3.project(to, axis));
        double angle = Vector3.angle(from, to);
        return angle * (Vector3.dot(axis, Vector3.cross(from, to)) < 0 ? -1 : 1);
    }

    public static Vector3 scale(Vector3 va, Vector3 vb){
        double x = va.x * vb.x;
        double y = va.y * vb.y;
        double z = va.z * vb.z;
        return new Vector3(x, y, z);
    }

    public static Vector3 cross(Vector3 lhs, Vector3 rhs){
        double x = lhs.y * rhs.z - lhs.z * rhs.y;
        double y = lhs.z * rhs.x - lhs.x * rhs.z;
        double z = lhs.x * rhs.y - lhs.y * rhs.x;
        return new Vector3(x,y,z);
    }

    public static Vector3 reflect(Vector3 inDirection, Vector3 inNormal){
        double num = -2 * Vector3.dot(inNormal, inDirection);
        inNormal = inNormal.mul(num);
        inNormal.add(inDirection);
        return inNormal;
    }

    public static Vector3 project(Vector3 vector, Vector3 onNormal){
        double num = onNormal.sqrMagnitude();
        if (num < 1.175494e-38) return Vector3.zero();
        double num2 = Vector3.dot(vector, onNormal);
        Vector3 v3 = onNormal.clone();
        v3.mul(num2/num);
        return v3;
    }

    public static Vector3 add(Vector3 va, Vector3 vb){
        return new Vector3(va.x + vb.x, va.y + vb.y, va.z + vb.z);
    }

    public static Vector3 sub(Vector3 va, Vector3 vb){
        return new Vector3(va.x - vb.x, va.y - vb.y, va.z - vb.z);
    }

    public static Vector3 mul(Vector3 va, double d)
    {
        return new Vector3(va.x * d, va.y * d, va.z * d);
    }

    public static Vector3 mul(Vector3 va, double rx, double ry, double rz, double rw){
        Vector3 vec = va.clone();
        vec.mulQuat(rx, ry, rz, rw);
        return vec;
    }

    public static Vector3 unm(Vector3 va){
        return new Vector3(-va.x, -va.y, -va.z);
    }

    public static boolean equals(Vector3 va, Vector3 vb){
        Vector3 vec = sub(va, vb);
        return vec.sqrMagnitude() < 1e-10;
    }

    public static Vector3 up(){return new Vector3(0, 1, 0);}
    public static Vector3 down(){return new Vector3(0, -1, 0);}
    public static Vector3 left(){return new Vector3(-1, 0, 0);}
    public static Vector3 right(){return new Vector3(1, 0, 0);}
    public static Vector3 forward(){return new Vector3(0, 0, 1);}
    public static Vector3 back(){return new Vector3(0, 0, -1);}
    public static Vector3 zero(){return new Vector3(0, 0, 0);}
    public static Vector3 one(){return new Vector3(1, 1, 1);}

    public static double angle(Vector3 from, Vector3 to){
        return Math.acos(clamp(dot(Vector3.normalize(from), Vector3.normalize(to)), -1, 1)) * Math.toDegrees(1);
    }

    public static double distance(Vector3 va, Vector3 vb) {
        return Math.sqrt(Math.pow(va.x - vb.x, 2) + Math.pow(va.y - vb.y, 2) + Math.pow(va.z - vb.z, 2));
    }

    public static Vector3 normalize(Vector3 vec){
        double num = Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);
        if (num > 1e-5)
            return new Vector3(vec.x/num, vec.y/num, vec.z/num);
        return new Vector3(0, 0, 0);
    }

    public static double dot(Vector3 lhs, Vector3 rhs){
        return lhs.x * rhs.x + lhs.y * rhs.y + lhs.z * rhs.z;
    }

    public static Vector3 Lerp(Vector3 from, Vector3 to, double t){
        t = Vector3.clamp(t, 0, 1);
        return new Vector3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }
}
