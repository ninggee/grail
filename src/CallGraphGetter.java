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
import util.Functions;

import java.io.PrintStream;
import java.util.*;

public class CallGraphGetter {

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

    public static void getCallers(String path, final String mainClass, final String classaName, final String method) {
        __init();

        String argsString = "-cp .;" + path + " -pp -validate " + mainClass;

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CHATransformer.v().transform();
                SootClass app = Scene.v().loadClassAndSupport(classaName);
                SootMethod target = app.getMethod(method);
                printPossbileCallers(target);
            }

        }));

        soot.Main.main(argsString.split(" "));
    }

    private static void printPossbileCallers(SootMethod target) {
        CallGraph cg = Scene.v().getCallGraph();

        Iterator sources = new Sources(cg.edgesInto(target));
        Set<SootMethod> callers = new HashSet<>();

        while(sources.hasNext()) {
            SootMethod src = (SootMethod)sources.next();
            if(!callers.contains(src)) {
                getCalledPostion(src, target, System.out);
                callers.add(src);
            }
        }
    }

    private static void getCalledPostion(SootMethod src, SootMethod target, PrintStream out) {


        JimpleBody jimpleBody = (JimpleBody)src.retrieveActiveBody();

        UnitGraph g = new BriefUnitGraph(jimpleBody);


        Iterator<Unit> it = g.iterator();

        while(it.hasNext()) {
            Stmt stmt = (Stmt)it.next();
            LineNumberTag lineNumberTag = (LineNumberTag)stmt.getTag("LineNumberTag");
            if(stmt.containsInvokeExpr()) {
                if(stmt.getInvokeExpr().getMethod().equals(target)) {
                    String srcClass = src.getDeclaringClass().getName();
                    String srcName = src.getName();
                    int linNumber = lineNumberTag == null ? 0 : lineNumberTag.getLineNumber();
                    out.println(linNumber + "\t" + srcClass + "\t" + srcName);
                }
            }
        }
    }

    public static void main(String[] args) {
        String path = "D:\\codes\\java\\scheduler\\scheduler\\src\\examples";
        getCallers(path, "account.Main", "account.Account", "void transfer(account.Account,double)");
    }

}
