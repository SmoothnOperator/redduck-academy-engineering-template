// 04-content-derived-factory/src/Factory.cs
// A content-derived factory. Fill in Create() and Derive(). Plain C#, no framework.
// Run with: dotnet run
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;

public record Item(Dictionary<string, string> Content, string Id);

public static class Factory
{
    // Return an Item holding the content and an id derived from its content (use Derive).
    public static Item Create(Dictionary<string, string> content)
    {
        return new Item(new Dictionary<string, string>(content), Derive(content));
    }

    // Recompute the id from the content alone, with no registry and no shared state,
    // so the demo passes.
    public static string Derive(Dictionary<string, string> content)
    {
        var sortedKeys = content.Keys.OrderBy(k => k, StringComparer.Ordinal);
        var sb = new StringBuilder();
        foreach (var key in sortedKeys)
        {
            sb.Append(key).Append(':').Append(content[key]).Append('\n');
        }
        using var sha256 = SHA256.Create();
        var bytes = Encoding.UTF8.GetBytes(sb.ToString());
        var hash = sha256.ComputeHash(bytes);
        return Convert.ToHexString(hash).ToLowerInvariant();
    }


    public static void Main()
    {
        var content = new Dictionary<string, string>
        {
            ["title"] = "Quarterly report",
            ["body"] = "Revenue held steady.",
            ["author"] = "avery",
        };

        var item = Create(content);

        // Second party holds only the content, in a different field order, and derives the id itself.
        var sameContent = new Dictionary<string, string>
        {
            ["author"] = "avery",
            ["body"] = "Revenue held steady.",
            ["title"] = "Quarterly report",
        };
        if (Derive(sameContent) != item.Id)
            throw new Exception("same content must derive the same id");

        // Tamper with one field. The derived id must no longer match the original.
        var tampered = new Dictionary<string, string>(content) { ["body"] = "Revenue tripled." };
        if (Derive(tampered) == item.Id)
            throw new Exception("changed content must change the id");

        Console.WriteLine("ok: " + item.Id);
    }
}
