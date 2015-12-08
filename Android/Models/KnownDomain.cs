using System;

namespace UniquePassword.AndroidApplication.Models
{
    public class KnownDomain : IEquatable<KnownDomain>, IComparable<KnownDomain>
    {
        public string Domain { get; set; }
        public int? MaxLength { get; set; }
        public string SpecialCharacters { get; set; }

        public int CompareTo(KnownDomain other)
        {
            if (other == null || other.Domain == null)
            {
                return 1;
            }

            if (Domain == null)
            {
                return -1;
            }

            return Domain.CompareTo(other.Domain);
        }

        public bool Equals(KnownDomain other)
        {
            if (other == null)
            {
                return false;
            }

            return string.Equals(Domain, other.Domain, StringComparison.OrdinalIgnoreCase);
        }

        public override bool Equals(object obj)
        {
            return Equals(obj as KnownDomain);
        }

        public override int GetHashCode()
        {
            return Domain.GetHashCode();
        }

        public override string ToString()
        {
            return Domain;
        }
    }
}