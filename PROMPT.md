# PROMPT.md — Demonstration Run Feature Prompt

Invoked as:

```
@planner Add planogram task template — POST /api/programmes/{id}/templates
to clone a standard set of PLANOGRAM tasks into a new store programme,
applying department assignments and default priorities from the template
definition. The programmes module must not write directly into the
activities module's tables; the intent to create tasks must be raised as
a cross-module event.
```

This is one of the four suggested features in Section 3.4 of the
capstone spec. It was chosen because it exercises the module-boundary
and event-bus rules most directly (failure modes #1 and #4 in the
client context), and because it lands in the `programmes` module this
submission's Project model/service were built for.
