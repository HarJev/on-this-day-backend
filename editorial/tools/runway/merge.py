import json,glob,os,shutil,sys
S=sys.argv[1]; out=S+'/merged'
shutil.rmtree(out,ignore_errors=True); shutil.copytree('content',out)
ev=json.load(open('content/events.json')); dy=json.load(open('content/daily-events.json'))
for b in sorted(glob.glob('editorial/batches/*-historical-events')):
    if os.path.exists(b+'/draft-events.json'):
        ev['events']+=json.load(open(b+'/draft-events.json'))['events']
        dy['days']+=json.load(open(b+'/draft-daily-events.json'))['days']
json.dump(ev,open(out+'/events.json','w'),indent=2); json.dump(dy,open(out+'/daily-events.json','w'),indent=2)
for f in glob.glob('editorial/batches/*/draft-questions/*.json'): shutil.copy(f,out+'/quizzes/questions/')
print(len(ev['events']),'events',len(dy['days']),'days')
