# The *TemporalEMF* metamodeling framework

*TemporalEMF* is a temporal metamodeling framework that adds native temporal support for models.
reusing well-known concepts from temporal languages.
In *TemporalMEF* models and can be subjected to temporal queries to retrieve the model contents at different points in time.
We have built our framework on top of the [Eclipse Modeling Framework (EMF)](https://www.eclipse.org/emf/).
Behind the scenes, the history of a model is transparently stored in a [HBase](http://hbase.apache.org/) database.