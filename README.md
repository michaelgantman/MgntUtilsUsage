# MgntUtilsUsage
This repository contains several demos that demonstrate various features of 
<a href="https://github.com/michaelgantman/Mgnt">Open Source MgntUtils library</a>.</p> At the moment this repo is work in progress. 
In particular, it is not well documented yet. However, if you are willing to dive into the code you will find some nice demos for several features.</p>
I am working the documentation though. And I recently completed writing a very in-depth article about how to use <b>Self-populating factory</b> feature to build extensible Multi-Stage Workflows for multiple data types. The article covers:

<ul>
  <li>How to design workflows that support new data types without modifying existing code</li>
  <li>How to add and reorder processing stages independently</li>
  <li>How to introduce conditional workflow logic without coupling stages or implementations</li>
  <li>How self-populating factories eliminate switch statements, manual registries, and configuration overhead</li>
</ul>
</p>
The article at first explain in detail what is <b>Self-populating factory pattern</b>, including a walk-through a runnable example in the MgntUtils repo. After that it goes into detail how to build the workflow based on that pattern with a walk-through the runnable real world like example in this repo. <br> Here is the link to the article: 
<a href="https://www.linkedin.com/pulse/infrastructure-extensible-multi-stage-workflows-across-gantman-0vu2f?utm_source=share&utm_medium=member_android&utm_campaign=share_via">Infrastructure for Extensible Multi-Stage Workflows Across Multiple Data Types</a> Read it, and clone this repo and you will get good idea on how to build extensible, well designed (I hope) workflow, and run the example from this repo. 
   