
package pomdp;
import pomdp.paramsIN.PomdpSolveParams;

public class testLP {
    public static void main(String[] args) {
        double[] t = {1,2,3,4,5,6,7,8,9,10};
        double[] alpha = {1.1,2.2,3.3,4.2,5.3,6.2,7.3,8.2,9.2,10.1};
        double[] witness = {0,1,2,3,4,5,6,7,8,9};
        double diff =0;
        PomdpSolveParams param = new PomdpSolveParams();
        param.sparse_epsilon = 1E-9;
        param.epsilon = 1E-8;
       /* AlphaList list = new AlphaList();
        list.action = 1;
        list.alpha = t;
        list.next =  null;
       region rg = new region();
       rg.findRegionPoint(alpha, alphaIN.AlphaList.next, witness, diff, param);*/
        
        
    }
}
