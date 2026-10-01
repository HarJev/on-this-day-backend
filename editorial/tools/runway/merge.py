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
# Connected-quiz batches keep drafts in draft-questions.json. Write each as its proposed pack,
# marked published so the validator applies publish-time checks, and apply proposed links.
for b in sorted(glob.glob('editorial/batches/*-connected-quiz')):
    if not os.path.exists(b+'/draft-questions.json'): continue
    pack=json.load(open(b+'/batch.json'))['quizPackFilenames'][0]
    qs=json.load(open(b+'/draft-questions.json'))
    for q in qs['questions']: q['publicationState']='published'
    json.dump(qs,open(out+'/quizzes/questions/'+pack,'w'),indent=2)
    for l in json.load(open(b+'/proposed-links.json'))['existingQuestionLinks']:
        p=out+'/'+l['canonicalPack'].removeprefix('content/'); d=json.load(open(p))
        for q in d['questions']:
            if q['id']==l['questionId']: q['relatedEventIds']=(q.get('relatedEventIds') or [])+l['addRelatedEventIds']
        json.dump(d,open(p,'w'),indent=2)
    print(b, len(qs['questions']), 'draft questions as', pack)
