package pedigree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Very small helper to output an SVG line chart for the simulation results.
 */
public class SimpleSVG {

    public static void write(String filename,
                              List<CoalescenceAnalyzer.Point> pop,
                              List<CoalescenceAnalyzer.Point> mothers,
                              List<CoalescenceAnalyzer.Point> fathers) throws IOException {
        double tmax = Math.max(pop.get(pop.size()-1).time(),
                               Math.max(mothers.get(0).time(), fathers.get(0).time()));
        double ymax = 0;
        for (CoalescenceAnalyzer.Point p : pop) ymax = Math.max(ymax, p.n());
        for (CoalescenceAnalyzer.Point p : mothers) ymax = Math.max(ymax, p.n());
        for (CoalescenceAnalyzer.Point p : fathers) ymax = Math.max(ymax, p.n());

        int width = 800;
        int height = 400;
        double xscale = width/tmax;
        double yscale = height/ymax;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))){
            bw.write("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\""+width+"\" height=\""+height+"\">\n");
            bw.write("<polyline fill=\"none\" stroke=\"blue\" points=\"");
            for (CoalescenceAnalyzer.Point p: pop){
                bw.write((p.time()*xscale)+","+(height-p.n()*yscale)+" ");
            }
            bw.write(""/>\n");

            bw.write("<polyline fill=\"none\" stroke=\"red\" points=\"");
            for (CoalescenceAnalyzer.Point p: mothers){
                bw.write((p.time()*xscale)+","+(height-p.n()*yscale)+" ");
            }
            bw.write(""/>\n");

            bw.write("<polyline fill=\"none\" stroke=\"green\" points=\"");
            for (CoalescenceAnalyzer.Point p: fathers){
                bw.write((p.time()*xscale)+","+(height-p.n()*yscale)+" ");
            }
            bw.write(""/>\n");
            bw.write("</svg>\n");
        }
    }
}
