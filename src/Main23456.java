import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.*;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import sun.security.krb5.SCDynamicStoreConfig;
import util.Functions;
import util.PetriNet;

import java.io.File;
import java.util.*;

    public class Main23456 {

    public static void main(String[] args) {
        String path = "src";
        Functions.initSoot(path);
        String classPath = "D:\\codes\\java\\scheduler\\scheduler\\src\\examples";

        String argsString = "-cp .;" + classPath + " -pp -validate " + "account.Main";
        System.out.println(argsString);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass acount = Scene.v().loadClassAndSupport("account.Account");
                SootMethod transfer = acount.getMethod("void transfer(account.Account,double)");
                Main23456.printPossbileCallers(transfer);
            }

        }));
        soot.Main.main(argsString.split(" "));
    }

    public static void printCallFromMethod(SootMethod method) {
        JimpleBody jimpleBody = (JimpleBody)method.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);

        Iterator<Unit> it = g.iterator();

        while(it.hasNext()) {
            Stmt stmt = (Stmt)it.next();
            LineNumberTag lineNumberTag = (LineNumberTag)stmt.getTag("LineNumberTag");
            if(stmt.containsInvokeExpr()) {
                //System.out.println("the invoke is : " + stmt.toString());
                if (lineNumberTag != null) {
                    System.out.print("In line " + lineNumberTag.getLineNumber());
                    System.out.println(" call method : " + stmt.getInvokeExpr().getMethod());
                }
            }
        }
    }

    public static void printPossbileCallers(SootMethod target) {
        CallGraph cg = Scene.v().getCallGraph();

        Iterator sources = new Sources(cg.edgesInto(target));
        Set<SootMethod> callers = new HashSet<>();

        while(sources.hasNext()) {
            SootMethod src = (SootMethod)sources.next();
            if(!callers.contains(src)) {
                getCalledPostion(src, target);
                callers.add(src);
            }
        }
    }

    public static void getCalledPostion(SootMethod src, SootMethod target) {
        JimpleBody jimpleBody = (JimpleBody)src.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);


        Iterator<Unit> it = g.iterator();

        while(it.hasNext()) {
            Stmt stmt = (Stmt)it.next();
            LineNumberTag lineNumberTag = (LineNumberTag)stmt.getTag("LineNumberTag");
            if(stmt.containsInvokeExpr()) {
                //System.out.println("the invoke is : " + stmt.toString());
//                System.out.println(stmt.getInvokeExpr().getMethod().getName());
                if(stmt.getInvokeExpr().getMethod().equals(target)) {
//                    System.out.print("In line " + lineNumberTag.getLineNumber());
                    System.out.println((lineNumberTag ==null ? "null" : lineNumberTag.getLineNumber()+ " ")  + src+ " call method : " + stmt.getInvokeExpr().getMethod());
                }

            }
        }
    }

    private static List<String> getAllDirectories(String path) {
        File directory = new File(path);

        File[] fileList = directory.listFiles();

        List<String> excludeList = new ArrayList<>();


        for (File f: fileList) {
            if(f.isFile()) {

            } else if (f.isDirectory()) {
                excludeList.add(f.getName() + ".");
            }
        }

        return excludeList;
    }
}