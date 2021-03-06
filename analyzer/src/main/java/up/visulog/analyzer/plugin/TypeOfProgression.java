package up.visulog.analyzer.plugin;

import up.visulog.analyzer.AnalyzerPlugin;
import up.visulog.analyzer.AnalyzerShape;
import up.visulog.analyzer.ChartTypes;
import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;

import java.util.*;

public class TypeOfProgression implements AnalyzerPlugin {

    private final Configuration configuration;

    private Result result;

    public TypeOfProgression(Configuration generalConfiguration) {
        this.configuration = generalConfiguration;
    }

    static Result processLog(List<Commit> gitLog) {
        var result = new Result();

        gitLog.sort(Comparator.comparing(o -> o.date.getTime()));
        int i = 1;
        for (var commit : gitLog) {
            result.resultsMap.put(commit.getFormattedDate(), i);
            i++;
        }

        return result;
    }

    @Override
    public void run() {
        this.result = processLog(Objects.requireNonNull(Commit.parseAllFromBranch(configuration.branch, configuration.start, configuration.end, configuration.aliases, configuration.mailBlacklist, configuration.mailWhitelist, configuration.format)));
    }

    @Override
    public Result getResult() {
        if (result == null) run();
        return result;
    }
    static class Result extends AnalyzerShape implements AnalyzerPlugin.Result{


        Result() {
            super("Type of project progression", ChartTypes.AREA);
        }

        @Override
        public String getResultAsString() { return this.resultsMap.toString(); }

        @Override
        public String getResultAsHtmlDiv() {
            StringBuilder html = new StringBuilder("<div>Type of project progression: \n <ul>\n");
            int a = 0;
            int max = -1;
            String maxDate = null;
            String s="";
            for (var item : this.resultsMap.entrySet()) {
                a = item.getValue();
                if (item.getValue() > max) {
                    max = item.getValue();
                    maxDate = item.getKey();
                }
                s= item.getKey();
            }
            html.append("<li> Date of last modification: ").append(s).append("</li>\n");
            html.append("<li> Number of all commits: ").append(a).append("</li>\n");
            html.append("</ul>\n</div>\n");

            return html.toString();
        }

        @Override
        public Map<String, Integer> getResults() {
            return this.resultsMap;
        }


        @Override
        public String getPluginName() {
            return this.pluginName;
        }

        @Override
        public String getChartType() {
            return this.chartType.type;
        }


    }

}