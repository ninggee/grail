package analysis;

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.PrintStream;
import java.util.*;

public class CallGraphGetter {
    public static boolean isFinished = false;

    public static Map<String, List<CallSite>> callsites = new HashMap();
    // 初始化soot参数
    private static void __init() {
        soot.G.reset();

        Options options = Options.v();
        //allow soot class creation from missing classes
        options.set_allow_phantom_refs(true);

        //prepend the given soot classpath to the default classpath
        //options.set_prepend_classpath(true);

        // run internal validation on bodies
        options.set_validate(true);

        // set output format for soot
        options.set_output_format(Options.output_format_none);

        // only java are accepted by soot analysis
        options.set_src_prec(Options.src_prec_class);

        // keep line number table, so you can access line number when later analysis
        options.set_keep_line_number(true);

        // attach bytecode offset to IR, we don't need this feature this project
        //options.set_keep_offset(true);

        // do not load bodies for excluded classes
        options.set_no_bodies_for_excluded(true);

        options.set_whole_program(true);
    }

    //获取某个方法的直接调用节点
    public static void getCallers(String path, final String mainClass, final String classaName, final String method) {
        __init();

        String argsString = "-cp .;" + path + " -pp -validate " + mainClass;

        // 设置程序的入口
        Options.v().parse(argsString.split(" "));
        SootClass c = Scene.v().forceResolve(mainClass, SootClass.BODIES);
        c.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        SootMethod m = c.getMethodByName("main");
        List entryPoints = new ArrayList();
        entryPoints.add(m);
        Scene.v().setEntryPoints(entryPoints);


        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass app = Scene.v().loadClassAndSupport(classaName);

                SootMethod target;
                try {
                    target = app.getMethod(method);
                } catch (RuntimeException e) {
                    String realMethodName = getMethodName(method);
                    if(realMethodName.equals(getClassName(classaName))) {
                        realMethodName = getConstructorFromMethodName(method);
                        target = app.getMethod(realMethodName);
                    } else {
                        target = app.getMethodByName(realMethodName);
                    }

                }
                callsites = printPossbileCallers(target);
                isFinished = true;
            }
        }));

        PackManager.v().runPacks();
    }

    // 获取某个方法的可能调用者
    private static Map<String, List<CallSite>> printPossbileCallers(SootMethod target) {
        CallGraph cg = Scene.v().getCallGraph();


        Map<String, List<CallSite>> results = new HashMap<>();
        Iterator sources = new Sources(cg.edgesInto(target));
        Set<SootMethod> callers = new HashSet<>();

        while(sources.hasNext()) {
            SootMethod src = (SootMethod)sources.next();
            if(!callers.contains(src)) {
                List<CallSite> result  = getCalledPostion(src, target, System.out);
                results.put(src.getSignature(), result);
                callers.add(src);
            }
        }

        return results;
    }

    // 获取详细的调用信息
    private static List<CallSite> getCalledPostion(SootMethod src, SootMethod target, PrintStream out) {

        JimpleBody jimpleBody = (JimpleBody)src.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);

        Iterator<Unit> it = g.iterator();

        List<CallSite> result = new ArrayList<>();

        while(it.hasNext()) {
            Stmt stmt = (Stmt)it.next();
            LineNumberTag lineNumberTag = (LineNumberTag)stmt.getTag("LineNumberTag");
            if(stmt.containsInvokeExpr()) {
                if(stmt.getInvokeExpr().getMethod().equals(target)) {
                    String srcClass = src.getDeclaringClass().getName();
                    String srcName = src.getName();
                    int linNumber = lineNumberTag == null ? 0 : lineNumberTag.getLineNumber();
                    result.add(new CallSite(linNumber, srcClass, src.getSignature()));
                }
            }
        }
        return  result;
    }

    //测试程序
    public static void main(String[] args) {
        String path = "D:\\codes\\java\\scheduler\\scheduler\\src\\examples\\linkedlist\\bin";
        getCallers(path, "linkedlist.BugTester", "linkedlist.MyListNode", "MyListNode(java.lang.Object,linkedlist.MyListNode)");

        Map<String, List<CallSite>> sites = callsites;

        for(String key : sites.keySet()) {
            List<CallSite> site = sites.get(key);

            for (CallSite s: site) {
                System.out.println(s.getMethod());
                System.out.println(s.getShortName());
                System.out.println(s.getSignature());
            }
        }
    }

    // 从传来的方法签名中获取函数名
    private static String getMethodName(String methodName) {
        String[] temp = methodName.split("\\(");
        temp = temp[0].split(" ");

        return temp[temp.length -1];
    }

    // 从完整的类命中，取出真正的类名
    private static String getClassName(String className) {
        String [] temp = className.split("\\.");
        return temp[temp.length - 1];
    }

    //将构造函数修改成soot可以接收的形式
    private static String getConstructorFromMethodName(String method) {
        String [] temp1 = method.split("\\(");

        String[] temp2 =  temp1[0].split(" ");
        temp2[temp2.length - 1] = "<init>";
        String result = "";

        for (String s : temp2) {
            result  = result + s  + " ";
        }
        result = result.substring(0, result.length() - 1);

        for (int i = 1; i < temp1.length; i++) {
            result += "(" + temp1[i];
        }

        result = result.replace(" ", "");


        result = "void " +  result;

        return result;
    }

}


