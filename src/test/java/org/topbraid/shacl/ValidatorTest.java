package org.topbraid.shacl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

public class ValidatorTest {

    Validator validator = null ;

    {
        try {
            validator = new Validator(getClass().getClassLoader().getResourceAsStream("journal-shape.dds.ttl"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void validatesValidJournalRDF() throws Exception {
        validator.validate(getClass().getClassLoader().getResourceAsStream("journal-example.ttl"));
    }

    @Test(expected=Exception.class)
    public void validatesInvalidJournalRDF() throws Exception {
        validator.validate(getClass().getClassLoader().getResourceAsStream("invalid-journal-example.ttl"));
    }

    @Test
    public void validatesValidJournalRDFWithMultithreading() throws Exception {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "50");
        IntStream.range(0, 200).parallel().forEach(f->{
            try {
                validator = new Validator(getClass().getClassLoader().getResourceAsStream("journal-shape.dds.ttl"));
                validator.validate(getClass().getClassLoader().getResourceAsStream("journal-example.ttl"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}