import java.io.File;
import java.util.Scanner;
import java.util.Vector;

public class Shell{
    public Shell(){
        File f = new File("src/commands");
        Scanner scan = null;
        try{
            scan = new Scanner(f);
        }
        catch (Exception e){
            System.out.println("ERROR in reading commands");
        }
        PM pm = new PM();
        pm.init(null);
        while(scan.hasNext()){
            String input = scan.nextLine().trim();
            System.out.println(">>"+input);
            String[] x=parseInput(input);
            do_instr(pm,x);
        }
        System.out.println("All commands executed");
    }
    String[] parseInput(String s){
        String[] ss=s.split(" ");
        return ss;
    }
    void do_instr(PM pm,String[] ins){
        if(ins[0].equals("init")){
            pm.init(ins);
            return;
        }
        if(ins[0].equals("cr")){
            pm.cr(ins);
            return;
        }
        if(ins[0].equals("de")){
            pm.de(ins);
            return;
        }
        if(ins[0].equals("req")){
            pm.req(ins);
            return;
        }
        if(ins[0].equals("rel")){
            pm.rel(ins);
            return;
        }
        if(ins[0].equals("to")){
            pm.to(ins);
            return;
        }
        if(ins[0].equals("list")){
            pm.list(ins);
            return;
        }
        if(ins[0].equals("pr")){
            pm.pr(ins);
            return;
        }
    }
};
