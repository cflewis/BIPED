require 'rake/clean'

CLEAN.include('problem', 'solutions', 'rules.lp')

LPARSE_FILES = FileList['ec.lp', 'engine.lp', 'rules.lp', 'context.lp']
PROLOG = 'swipl'

if not ENV['file'].nil?
    GAME_FILE = ENV['file']
else
    GAME_FILE = 'script.pl'
end

if not ENV['models'].nil?
    MODELS = ENV['models']
else
    MODELS = '10'
end

task :default => 'solution'

task 'rules.lp' => GAME_FILE do
    sh "#{PROLOG} -g '[compiler,script], main.' > rules.lp"
end

task 'problem' => LPARSE_FILES do
    sh "gringo -c t_max=5 #{LPARSE_FILES.to_s()} " +
        "| pv -N grounding > problem"
end

task 'solution' => ['problem'] do
    sh %{pv -N solving problem | clasp -n #{MODELS} > solution} do | ok, status |
        if status.exitstatus == 10 then
            puts "Satisfied"
        else
            fail "Unsatisfiable"
        end
    end
end
