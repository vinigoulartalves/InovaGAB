namespace InovaGAB.Infrastructure.Common;

public static class QueryPaging
{
    public static (int Page, int PageSize) Normalize(int page, int pageSize)
    {
        var p = page < 1 ? 1 : page;
        var ps = pageSize < 1 ? 20 : Math.Min(pageSize, 100);
        return (p, ps);
    }
}
