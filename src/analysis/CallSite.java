package analysis;

public class CallSite {
    public int line;
    public String className;
    public String method;

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

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "In " + getLine() + " " + getClassName() + " " + getMethod();
    }
}