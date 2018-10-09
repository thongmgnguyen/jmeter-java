package com.sample;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbsKatalonSamplerClient implements JavaSamplerClient {

    protected abstract void executeKatalon(JavaSamplerContext context);


    public void setupTest(JavaSamplerContext context) {
    }

    public SampleResult runTest(JavaSamplerContext context) {

        executeKatalon(context);

        String reportDir = context.getParameter("REPORT_DIR");
        final SampleResult[] result = {null};
        try {
            Files.find(Paths.get(reportDir), Integer.MAX_VALUE, (path, attributes) -> path.toString().endsWith(".har"))
                .forEach(path -> {
                    List<SampleResult> subs = parseHar(path);

                    if (!subs.isEmpty()) {
                        if (result[0] == null) {
                            result[0] = subs.get(0);
                            subs.remove(0);
                        }

                        subs.forEach(sub -> {
                            String name = sub.getSampleLabel();
                            result[0].addRawSubResult(sub);
                            sub.setSampleLabel(name);
                        });

                    }
                });



        } catch (IOException e) {
            e.printStackTrace();
        }
        return result[0];
    }


    private List<SampleResult> parseHar(Path filePath) {
        List<SampleResult> results = new ArrayList<>();
        try {
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(filePath.toFile());
            List<HarEntry> entries = har.getLog().getEntries();

            for (HarEntry entry : entries) {
                HarRequest request = entry.getRequest();
                HarResponse response = entry.getResponse();
                HarContent content = response.getContent();
                HarTiming timing = entry.getTimings();

                SampleResult rs = new SampleResult();
                Integer latency = timing.getWait();
                rs.setLatency(latency);
                rs.setStampAndTime(entry.getStartedDateTime().getTime(), entry.getTime());
                rs.setSampleLabel(request.getUrl());
                rs.setConnectTime(timing.getSend());
                rs.setSuccessful(true);
                rs.setBodySize(response.getBodySize());
                rs.setBytes(content.getSize());
                rs.setContentType(content.getMimeType());
                rs.setDataEncoding(content.getEncoding());
                rs.setIdleTime(timing.getBlocked());
                rs.setResponseCode(String.valueOf(response.getStatus()));
                rs.setResponseMessage(response.getStatusText());
                rs.setURL(new URL(request.getUrl()));
                rs.setSentBytes(request.getBodySize());
                rs.setHeadersSize(request.getHeadersSize().intValue());
                results.add(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public void teardownTest(JavaSamplerContext context) {

    }

    public Arguments getDefaultParameters() {
        return null;
    }
}
