package com.jun.plugin.common.run;

@FunctionalInterface
public interface IInitRunner {

        /**
         * Callback used to run the bean.
         * @throws Exception on error
         */
        void run() throws Exception;

}
