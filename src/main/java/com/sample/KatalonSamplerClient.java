package com.sample;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

public class KatalonSamplerClient extends AbsKatalonSamplerClient {
    @Override
    protected void executeKatalon(JavaSamplerContext context) {
        System.out.println("execute");
    }
}
