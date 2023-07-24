package com.gitthub.wujun728.engine.controller;

import com.gitthub.wujun728.engine.bytecodes.service.ExecuteStringSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/class")
public class RunCodeController {
    private Logger logger = LoggerFactory.getLogger(RunCodeController.class);

    @Autowired
    private ExecuteStringSourceService executeStringSourceService;


    private static final String defaultSource = "public class Run {\n"
            + "    public static void main(String[] args) {\n"
            + "        \n"
            + "    }\n"
            + "}";

    @RequestMapping(path = {"/code"}, method = RequestMethod.GET)
    @ResponseBody
    public String entry(Model model) {
        model.addAttribute("lastSource", defaultSource);
        return defaultSource;
    }

    @RequestMapping(path = {"/code/run"}, method = RequestMethod.POST)
    @ResponseBody
    public String runCode(@RequestParam("source") String source,
                          @RequestParam("systemIn") String systemIn, Model model, HttpServletRequest request) {

        String runResult = executeStringSourceService.execute2(source, systemIn);
        runResult = runResult.replaceAll(System.lineSeparator(), "<br/>"); // 处理html中换行的问题
		model.addAttribute("lastSource", source);
        model.addAttribute("lastSystemIn", systemIn);
        model.addAttribute("runResult", runResult);
        //return "ide.html";
        return runResult;
    }
}
