package up.visulog.analyzer.plugin;

import up.visulog.analyzer.AnalyzerPlugin;
import up.visulog.analyzer.AnalyzerShape;
import up.visulog.analyzer.ChartTypes;
import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is an analyzer's plugin which goal is to count the
 * number of merge commits per author in a given cloned repo.
 */
public class CountMergeCommitsPerAuthor implements AnalyzerPlugin {
    /** The configuration to use. */
    private final Configuration configuration;

    /** The result obtained after the computation. */
    private Result result;


    /**
     * Class constructor.
     *
     * @param generalConfiguration The general configuration of the analyzer.
     */
    public CountMergeCommitsPerAuthor(Configuration generalConfiguration) {
        this.configuration = generalConfiguration;
    }

    /**
     * A task which processes the logs to obtain the count of merge commits per author.
     *
     * @param gitLog A list of commits in the given repo (see gitrawdata)/
     * @return the result of the computation.
     */
    static Result processLog(List<Commit> gitLog) {
        var result = new Result();

        for (var commit : gitLog) {
            if (commit.description.toLowerCase().contains("merge branch")) {
                var nb = result.resultsMap.getOrDefault(commit.author.getPrimaryName(), 0);
                result.resultsMap.put(commit.author.getPrimaryName(), nb + 1);
            }
        }

        return result;
    }

    /** Run this analyzer plugin. */
    @Override
    public void run() {
        this.result = processLog(Objects.requireNonNull(Commit.parseAllFromBranch(configuration.branch, configuration.start, configuration.end, configuration.aliases, configuration.mailBlacklist, configuration.mailWhitelist, configuration.format)));
    }

    /** @return the result of this analysis. Runs the analysis first if not already done. */
    @Override
    public Result getResult() {
        if (result == null) run();
        return result;
    }



    /** This is the result class for this plugin. */
    static class Result extends AnalyzerShape implements AnalyzerPlugin.Result {

        public Result() {
            super("Count merge commits per author", ChartTypes.COLUMN);
        }

        /** Get the hash map containing the count of commits per author. */
        public Map<String, Integer> getResults() {
            return this.resultsMap;
        }

        /**
         * @return the plugin name
         */
        @Override
        public String getPluginName() {
            return this.pluginName;
        }

        @Override
        public String getChartType() {
            return this.chartType.type;
        }

        /** @return the result of this analysis, as a string. */
        @Override
        public String getResultAsString() {
            return this.resultsMap.toString();
        }

        /** @return the result of this analysis, as an HTML div (which can be use to render an .html file). */
        @Override
        public String getResultAsHtmlDiv() {
            StringBuilder html = new StringBuilder("<div>Merge commits per author: \n<ul>\n");

            for (var item : this.resultsMap.entrySet()) {
                html.append("<li>").append(item.getKey()).append(": ").append(item.getValue()).append("</li>\n");
            }

            html.append("</ul>\n</div>\n");

            return html.toString();
        }
    }
}