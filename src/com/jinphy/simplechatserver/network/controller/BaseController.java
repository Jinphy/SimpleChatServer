package com.jinphy.simplechatserver.network.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DESC:
 * Created by jinphy on 2018/1/16.
 */
public class BaseController {
    protected static ExecutorService threadPools = Executors.newCachedThreadPool();
}
