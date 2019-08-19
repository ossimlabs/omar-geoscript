package omar.geoscript


class ProjectionCache 
{
    static List getList()
    {
        def records = []
        def stream = ProjectionCache.getResourceAsStream('/omar/geoscript/projs.csv')

        stream?.eachLine { line ->
            def tokens = line.split(',')
            records << [id: tokens[0], units: tokens[1]]
        }

        stream?.close()
        records
    }
}