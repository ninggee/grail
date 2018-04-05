package analysis;

public class CallSite {
    public int line;
    public String className;
    public String method;
    public String signature;
    public String shortName;

    public CallSite(int line, String className, String method) {
        this.line = line;
        this.className = className;
        this.method = method;
    }

    // 0代表没有获取到
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public String GetFullMethodName() {
        return method;
    }

    public String getSignature() {
        if(signature == null) {
            signature = method.substring(method.indexOf(":") + 2, method.length() -  1);
        }

        return signature;
    }

    public String getShortName() {
        if(shortName == null) {
            String signature = this.getSignature();
            shortName = signature.substring(0, signature.indexOf("("));
            String [] temp = shortName.split(" ");
            shortName = temp[temp.length - 1];
        }

        return shortName;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "In " + getLine() + " " + getClassName() + " " + getMethod();
    }
}