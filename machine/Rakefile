require 'rake/clean'

CLEAN.include('problem', 'solutions', 'rules.lp')

LPARSE_FILES = FileList['ec.lp', 'engine.lp', 'rules.lp', 'context.lp']
PROLOG = 'swipl'

if ENV['file'].nil?
    GAME_FILE = 'script.pl'
else
    GAME_FILE = ENV['file']
end

task :default => 'solution'

task 'rules.lp' => GAME_FILE do
    sh "#{PROLOG} -g '[compiler,script], main.' > rules.lp"
end

task 'problem' => LPARSE_FILES do
    sh "lparse --true-negation -c t_max=5 #{LPARSE_FILES.to_s()} " +
        "| pv -N grounding > problem"
end

task 'solution' => ['problem'] do
    sh %{pv -N solving problem | clasp -n 10 > solution} do | ok, status |
        if status.exitstatus == 10 then
            puts "Satisfied"
        else
            fail "Unsatisfiable"
        end
    end
end
