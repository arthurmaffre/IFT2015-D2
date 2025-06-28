package pedigree;

import org.junit.Test;
import static org.junit.Assert.*;
import pedigree.Sim;

public class simtest {

    @Test
    public void testAddition() {
        Sim sim = new Sim();
        int result = sim.add(2, 3);
        assertEquals(5, result);
    }
}