package com.sample;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {


    public static void main(String[] agrs) throws IOException {
        //JMeter Engine
        StandardJMeterEngine jmeter = new StandardJMeterEngine();

        //JMeter initialization (properties, log levels, locale, etc)
        JMeterUtils.loadJMeterProperties("D:\\apache-jmeter-5.0\\bin\\jmeter.properties");
        JMeterUtils.setJMeterHome("D:\\apache-jmeter-5.0");
        JMeterUtils.initLocale();

        // JMeter Test Plan
        HashTree testPlanTree = new HashTree();

        // HTTP Sampler
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setName("HTTP Request");
        httpSampler.setProtocol(HTTPConstantsInterface.PROTOCOL_HTTPS);
        httpSampler.setDomain("jira.katalon.com/rest/api/2/issue/KD-1");
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");

        JavaSampler javaSampler = new JavaSampler();
        javaSampler.setClassname(KatalonSamplerClient.class.getName());
        javaSampler.setName("Java sampler");

        Arguments args = new Arguments();
        args.addArgument("REPORT_DIR", "D:\\JMeterJava\\requests\\");
        javaSampler.setArguments(args);



        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(1);
        loopController.setFirst(true);
        loopController.initialize();

        LoopController loop = new LoopController();
        loop.setLoops(1);
        loop.setFirst(true);
        loop.initialize();
        loop.addTestElement(httpSampler);

        // Thread Group

        org.apache.jmeter.threads.ThreadGroup threadGroup = new org.apache.jmeter.threads.ThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setRampUp(1);
        threadGroup.setName("Main Thread Group");
        threadGroup.setSamplerController(loopController);

        // Test Plan
        TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Construct Test Plan from previously initialized elements
        testPlanTree.add(testPlan);
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
//        threadGroupHashTree.add(httpSampler);
        threadGroupHashTree.add(javaSampler);
        threadGroupHashTree.add(loop);


        // save generated test plan to JMeter's .jmx file format
//        SaveService.saveTree(testPlanTree, new FileOutputStream("test.jmx"));

        //add Summarizer output to get test progress in stdout like:
        // summary =      2 in   1.3s =    1.5/s Avg:   631 Min:   290 Max:   973 Err:     0 (0.00%)
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }




        // Store execution results into a .jtl file
        String logFile = "test.csv";
        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename(logFile);
        testPlanTree.add(testPlanTree.getArray()[0], logger);

        // Run Test Plan
        jmeter.configure(testPlanTree);
        jmeter.run();


        try {
            ReportGenerator reportGen = new ReportGenerator(logFile, null);
            reportGen.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Test completed. See test.jtl file for results");
        System.out.println("Open test.jmx file in JMeter GUI to validate the code");
    }
}
