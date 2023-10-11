# Throttled Action Queue
The implementation is intended to execute a queue of 'Runnable' actions that need to be performed one after the other
with a fixed delay between actions.

At the end of the execution of the queue actions, the final listener will be executed, which is also a 'Runnable' action.

The configurable time refers to the delay time and its settings are both in frequency and in unit of time.