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

public class Tools {
    public static CallSite containMethod = null;
    public static boolean isFind = false;

    public static void getContainMethod(String path, String mainClass, final String className, int line) {

        __init();

        String argsString = "-cp .;" + path + " -pp -validate " + className;
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

                SootClass app = Scene.v().loadClassAndSupport(className);

                Iterator<SootMethod> methodIterator = app.methodIterator();

                while(methodIterator.hasNext()) {
                    SootMethod m = (SootMethod)methodIterator.next();

                    if(isContain(m, line)) {
                        containMethod = new CallSite(line, app.getName(), m.getSignature());
                        isFind = true;
                        break;
                    }
                }
            }
        }));

        PackManager.v().runPacks();

//        soot.Main.main(argsString.split(" "));
    }

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

    private static boolean isContain(SootMethod m, int line) {
        JimpleBody jimpleBody = (JimpleBody)m.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);

        List<Unit> heads = g.getHeads();

        Stmt head = (Stmt) heads.get(0);
        LineNumberTag lineNumberTag = (LineNumberTag)head.getTag("LineNumberTag");

        int start = lineNumberTag.getLineNumber();
//        System.out.println(m.getName() + start);

        List<Unit> tails = g.getTails();

        Stmt tail = (Stmt)tails.get(tails.size() - 1);

        lineNumberTag = (LineNumberTag)tail.getTag("LineNumberTag");

        int end = lineNumberTag.getLineNumber();
//        System.out.println(m.getName() + end);

        return line >= start && line <= end;


    }

    public static void main(String[] args) {
        String path = "D:\\codes\\java\\scheduler\\scheduler\\src\\examples\\linkedlist\\bin";
        getContainMethod(path, "linkedlist.BugTester", "linkedlist.MyListNode", 21);


       System.out.println(containMethod.getSignature());
    }





}
