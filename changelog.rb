source = ARGV[0] || 'master'
puts `git shortlog --no-merges #{source}...HEAD`
